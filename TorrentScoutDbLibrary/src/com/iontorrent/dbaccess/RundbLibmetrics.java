/*
*	Copyright (C) 2011 Life Technologies Inc.
*
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 2 of the License, or
*   (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.iontorrent.dbaccess;


import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
/**
 *
 * @author Chantal Roth
 */
@Entity
@Table(name = "rundb_libmetrics")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RundbLibmetrics.findAll", query = "SELECT r FROM RundbLibmetrics r")})
public class RundbLibmetrics implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "\"id\"")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "\"sysSNR\"")
    private double sysSNR;
    @Basic(optional = false)
    @Column(name = "\"aveKeyCounts\"")
    private double aveKeyCounts;
    @Basic(optional = false)
    @Column(name = "\"totalNumReads\"")
    private int totalNumReads;
    @Basic(optional = false)
    @Column(name = "\"genomelength\"")
    private int genomelength;
    @Basic(optional = false)
    @Column(name = "\"rNumAlignments\"")
    private int rNumAlignments;
    @Basic(optional = false)
    @Column(name = "\"rMeanAlignLen\"")
    private int rMeanAlignLen;
    @Basic(optional = false)
    @Column(name = "\"rLongestAlign\"")
    private int rLongestAlign;
    @Basic(optional = false)
    @Column(name = "\"rCoverage\"")
    private double rCoverage;
    @Basic(optional = false)
    @Column(name = "\"r50Q10\"")
    private int r50Q10;
    @Basic(optional = false)
    @Column(name = "\"r100Q10\"")
    private int r100Q10;
    @Basic(optional = false)
    @Column(name = "\"r200Q10\"")
    private int r200Q10;
    @Basic(optional = false)
    @Column(name = "\"r50Q17\"")
    private int r50Q17;
    @Basic(optional = false)
    @Column(name = "\"r100Q17\"")
    private int r100Q17;
    @Basic(optional = false)
    @Column(name = "\"r200Q17\"")
    private int r200Q17;
    @Basic(optional = false)
    @Column(name = "\"r50Q20\"")
    private int r50Q20;
    @Basic(optional = false)
    @Column(name = "\"r100Q20\"")
    private int r100Q20;
    @Basic(optional = false)
    @Column(name = "\"r200Q20\"")
    private int r200Q20;
    @Basic(optional = false)
    @Column(name = "\"sNumAlignments\"")
    private int sNumAlignments;
    @Basic(optional = false)
    @Column(name = "\"sMeanAlignLen\"")
    private int sMeanAlignLen;
    @Basic(optional = false)
    @Column(name = "\"sLongestAlign\"")
    private int sLongestAlign;
    @Basic(optional = false)
    @Column(name = "\"sCoverage\"")
    private double sCoverage;
    @Basic(optional = false)
    @Column(name = "\"s50Q10\"")
    private int s50Q10;
    @Basic(optional = false)
    @Column(name = "\"s100Q10\"")
    private int s100Q10;
    @Basic(optional = false)
    @Column(name = "\"s200Q10\"")
    private int s200Q10;
    @Basic(optional = false)
    @Column(name = "\"s50Q17\"")
    private int s50Q17;
    @Basic(optional = false)
    @Column(name = "\"s100Q17\"")
    private int s100Q17;
    @Basic(optional = false)
    @Column(name = "\"s200Q17\"")
    private int s200Q17;
    @Basic(optional = false)
    @Column(name = "\"s50Q20\"")
    private int s50Q20;
    @Basic(optional = false)
    @Column(name = "\"s100Q20\"")
    private int s100Q20;
    @Basic(optional = false)
    @Column(name = "\"s200Q20\"")
    private int s200Q20;
    @Basic(optional = false)
    @Column(name = "\"q7_coverage_percentage\"")
    private double q7CoveragePercentage;
    @Basic(optional = false)
    @Column(name = "\"q7_alignments\"")
    private int q7Alignments;
    @Basic(optional = false)
    @Column(name = "\"q7_mapped_bases\"")
    private int q7MappedBases;
    @Basic(optional = false)
    @Column(name = "\"q7_qscore_bases\"")
    private int q7QscoreBases;
    @Basic(optional = false)
    @Column(name = "\"q7_mean_alignment_length\"")
    private int q7MeanAlignmentLength;
    @Basic(optional = false)
    @Column(name = "\"q7_longest_alignment\"")
    private int q7LongestAlignment;
    @Basic(optional = false)
    @Column(name = "\"i50Q7_reads\"")
    private int i50Q7reads;
    @Basic(optional = false)
    @Column(name = "\"i100Q7_reads\"")
    private int i100Q7reads;
    @Basic(optional = false)
    @Column(name = "\"i200Q7_reads\"")
    private int i200Q7reads;
    @Basic(optional = false)
    @Column(name = "\"q10_coverage_percentage\"")
    private double q10CoveragePercentage;
    @Basic(optional = false)
    @Column(name = "\"q10_alignments\"")
    private int q10Alignments;
    @Basic(optional = false)
    @Column(name = "\"q10_mapped_bases\"")
    private int q10MappedBases;
    @Basic(optional = false)
    @Column(name = "\"q10_qscore_bases\"")
    private int q10QscoreBases;
    @Basic(optional = false)
    @Column(name = "\"q10_mean_alignment_length\"")
    private int q10MeanAlignmentLength;
    @Basic(optional = false)
    @Column(name = "\"q10_longest_alignment\"")
    private int q10LongestAlignment;
    @Basic(optional = false)
    @Column(name = "\"i50Q10_reads\"")
    private int i50Q10reads;
    @Basic(optional = false)
    @Column(name = "\"i100Q10_reads\"")
    private int i100Q10reads;
    @Basic(optional = false)
    @Column(name = "\"i200Q10_reads\"")
    private int i200Q10reads;
    @Basic(optional = false)
    @Column(name = "\"q17_coverage_percentage\"")
    private double q17CoveragePercentage;
    @Basic(optional = false)
    @Column(name = "\"q17_alignments\"")
    private int q17Alignments;
    @Basic(optional = false)
    @Column(name = "\"q17_mapped_bases\"")
    private int q17MappedBases;
    @Basic(optional = false)
    @Column(name = "\"q17_qscore_bases\"")
    private int q17QscoreBases;
    @Basic(optional = false)
    @Column(name = "\"q17_mean_alignment_length\"")
    private int q17MeanAlignmentLength;
    @Basic(optional = false)
    @Column(name = "\"q17_longest_alignment\"")
    private int q17LongestAlignment;
    @Basic(optional = false)
    @Column(name = "\"i50Q17_reads\"")
    private int i50Q17reads;
    @Basic(optional = false)
    @Column(name = "\"i100Q17_reads\"")
    private int i100Q17reads;
    @Basic(optional = false)
    @Column(name = "\"i200Q17_reads\"")
    private int i200Q17reads;
    @Basic(optional = false)
    @Column(name = "\"q20_coverage_percentage\"")
    private double q20CoveragePercentage;
    @Basic(optional = false)
    @Column(name = "\"q20_alignments\"")
    private int q20Alignments;
    @Basic(optional = false)
    @Column(name = "\"q20_mapped_bases\"")
    private int q20MappedBases;
    @Basic(optional = false)
    @Column(name = "\"q20_qscore_bases\"")
    private int q20QscoreBases;
    @Basic(optional = false)
    @Column(name = "\"q20_mean_alignment_length\"")
    private int q20MeanAlignmentLength;
    @Basic(optional = false)
    @Column(name = "\"q20_longest_alignment\"")
    private int q20LongestAlignment;
    @Basic(optional = false)
    @Column(name = "\"i50Q20_reads\"")
    private int i50Q20reads;
    @Basic(optional = false)
    @Column(name = "\"i100Q20_reads\"")
    private int i100Q20reads;
    @Basic(optional = false)
    @Column(name = "\"i200Q20_reads\"")
    private int i200Q20reads;
    @Basic(optional = false)
    @Column(name = "\"q47_coverage_percentage\"")
    private double q47CoveragePercentage;
    @Basic(optional = false)
    @Column(name = "\"q47_mapped_bases\"")
    private int q47MappedBases;
    @Basic(optional = false)
    @Column(name = "\"q47_qscore_bases\"")
    private int q47QscoreBases;
    @Basic(optional = false)
    @Column(name = "\"q47_alignments\"")
    private int q47Alignments;
    @Basic(optional = false)
    @Column(name = "\"q47_mean_alignment_length\"")
    private int q47MeanAlignmentLength;
    @Basic(optional = false)
    @Column(name = "\"q47_longest_alignment\"")
    private int q47LongestAlignment;
    @Basic(optional = false)
    @Column(name = "\"i50Q47_reads\"")
    private int i50Q47reads;
    @Basic(optional = false)
    @Column(name = "\"i100Q47_reads\"")
    private int i100Q47reads;
    @Basic(optional = false)
    @Column(name = "\"i200Q47_reads\"")
    private int i200Q47reads;
    @Basic(optional = false)
    @Column(name = "\"cf\"")
    private double cf;
    @Basic(optional = false)
    @Column(name = "\"ie\"")
    private double ie;
    @Basic(optional = false)
    @Column(name = "\"dr\"")
    private double dr;
    @Basic(optional = false)
    @Column(name = "\"Genome_Version\"")
    private String genomeVersion;
    @Basic(optional = false)
    @Column(name = "\"Index_Version\"")
    private String indexVersion;
    @Basic(optional = false)
    @Column(name = "\"align_sample\"")
    private int alignSample;
    @Basic(optional = false)
    @Column(name = "\"genome\"")
    private String genome;
    @Basic(optional = false)
    @Column(name = "\"genomesize\"")
    private long genomesize;
    @Basic(optional = false)
    @Column(name = "\"total_number_of_sampled_reads\"")
    private int totalNumberOfSampledReads;
    @Basic(optional = false)
    @Column(name = "\"sampled_q7_coverage_percentage\"")
    private double sampledQ7CoveragePercentage;
    @Basic(optional = false)
    @Column(name = "\"sampled_q7_mean_coverage_depth\"")
    private double sampledQ7MeanCoverageDepth;
    @Basic(optional = false)
    @Column(name = "\"sampled_q7_alignments\"")
    private int sampledQ7Alignments;
    @Basic(optional = false)
    @Column(name = "\"sampled_q7_mean_alignment_length\"")
    private int sampledQ7MeanAlignmentLength;
    @Basic(optional = false)
    @Column(name = "\"sampled_mapped_bases_in_q7_alignments\"")
    private int sampledMappedBasesInQ7Alignments;
    @Basic(optional = false)
    @Column(name = "\"sampled_q7_longest_alignment\"")
    private int sampledQ7LongestAlignment;
    @Basic(optional = false)
    @Column(name = "\"sampled_50q7_reads\"")
    private int sampled50q7Reads;
    @Basic(optional = false)
    @Column(name = "\"sampled_100q7_reads\"")
    private int sampled100q7Reads;
    @Basic(optional = false)
    @Column(name = "\"sampled_200q7_reads\"")
    private int sampled200q7Reads;
    @Basic(optional = false)
    @Column(name = "\"sampled_q10_coverage_percentage\"")
    private double sampledQ10CoveragePercentage;
    @Basic(optional = false)
    @Column(name = "\"sampled_q10_mean_coverage_depth\"")
    private double sampledQ10MeanCoverageDepth;
    @Basic(optional = false)
    @Column(name = "\"sampled_q10_alignments\"")
    private int sampledQ10Alignments;
    @Basic(optional = false)
    @Column(name = "\"sampled_q10_mean_alignment_length\"")
    private int sampledQ10MeanAlignmentLength;
    @Basic(optional = false)
    @Column(name = "\"sampled_mapped_bases_in_q10_alignments\"")
    private int sampledMappedBasesInQ10Alignments;
    @Basic(optional = false)
    @Column(name = "\"sampled_q10_longest_alignment\"")
    private int sampledQ10LongestAlignment;
    @Basic(optional = false)
    @Column(name = "\"sampled_50q10_reads\"")
    private int sampled50q10Reads;
    @Basic(optional = false)
    @Column(name = "\"sampled_100q10_reads\"")
    private int sampled100q10Reads;
    @Basic(optional = false)
    @Column(name = "\"sampled_200q10_reads\"")
    private int sampled200q10Reads;
    @Basic(optional = false)
    @Column(name = "\"sampled_q17_coverage_percentage\"")
    private double sampledQ17CoveragePercentage;
    @Basic(optional = false)
    @Column(name = "\"sampled_q17_mean_coverage_depth\"")
    private double sampledQ17MeanCoverageDepth;
    @Basic(optional = false)
    @Column(name = "\"sampled_q17_alignments\"")
    private int sampledQ17Alignments;
    @Basic(optional = false)
    @Column(name = "\"sampled_q17_mean_alignment_length\"")
    private int sampledQ17MeanAlignmentLength;
    @Basic(optional = false)
    @Column(name = "\"sampled_mapped_bases_in_q17_alignments\"")
    private int sampledMappedBasesInQ17Alignments;
    @Basic(optional = false)
    @Column(name = "\"sampled_q17_longest_alignment\"")
    private int sampledQ17LongestAlignment;
    @Basic(optional = false)
    @Column(name = "\"sampled_50q17_reads\"")
    private int sampled50q17Reads;
    @Basic(optional = false)
    @Column(name = "\"sampled_100q17_reads\"")
    private int sampled100q17Reads;
    @Basic(optional = false)
    @Column(name = "\"sampled_200q17_reads\"")
    private int sampled200q17Reads;
    @Basic(optional = false)
    @Column(name = "\"sampled_q20_coverage_percentage\"")
    private double sampledQ20CoveragePercentage;
    @Basic(optional = false)
    @Column(name = "\"sampled_q20_mean_coverage_depth\"")
    private double sampledQ20MeanCoverageDepth;
    @Basic(optional = false)
    @Column(name = "\"sampled_q20_alignments\"")
    private int sampledQ20Alignments;
    @Basic(optional = false)
    @Column(name = "\"sampled_q20_mean_alignment_length\"")
    private int sampledQ20MeanAlignmentLength;
    @Basic(optional = false)
    @Column(name = "\"sampled_mapped_bases_in_q20_alignments\"")
    private int sampledMappedBasesInQ20Alignments;
    @Basic(optional = false)
    @Column(name = "\"sampled_q20_longest_alignment\"")
    private int sampledQ20LongestAlignment;
    @Basic(optional = false)
    @Column(name = "\"sampled_50q20_reads\"")
    private int sampled50q20Reads;
    @Basic(optional = false)
    @Column(name = "\"sampled_100q20_reads\"")
    private int sampled100q20Reads;
    @Basic(optional = false)
    @Column(name = "\"sampled_200q20_reads\"")
    private int sampled200q20Reads;
    @Basic(optional = false)
    @Column(name = "\"sampled_q47_coverage_percentage\"")
    private double sampledQ47CoveragePercentage;
    @Basic(optional = false)
    @Column(name = "\"sampled_q47_mean_coverage_depth\"")
    private double sampledQ47MeanCoverageDepth;
    @Basic(optional = false)
    @Column(name = "\"sampled_q47_alignments\"")
    private int sampledQ47Alignments;
    @Basic(optional = false)
    @Column(name = "\"sampled_q47_mean_alignment_length\"")
    private int sampledQ47MeanAlignmentLength;
    @Basic(optional = false)
    @Column(name = "\"sampled_mapped_bases_in_q47_alignments\"")
    private int sampledMappedBasesInQ47Alignments;
    @Basic(optional = false)
    @Column(name = "\"sampled_q47_longest_alignment\"")
    private int sampledQ47LongestAlignment;
    @Basic(optional = false)
    @Column(name = "\"sampled_50q47_reads\"")
    private int sampled50q47Reads;
    @Basic(optional = false)
    @Column(name = "\"sampled_100q47_reads\"")
    private int sampled100q47Reads;
    @Basic(optional = false)
    @Column(name = "\"sampled_200q47_reads\"")
    private int sampled200q47Reads;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_from_number_of_sampled_reads\"")
    private int extrapolatedFromNumberOfSampledReads;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q7_coverage_percentage\"")
    private double extrapolatedQ7CoveragePercentage;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q7_mean_coverage_depth\"")
    private double extrapolatedQ7MeanCoverageDepth;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q7_alignments\"")
    private int extrapolatedQ7Alignments;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q7_mean_alignment_length\"")
    private int extrapolatedQ7MeanAlignmentLength;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_mapped_bases_in_q7_alignments\"")
    private int extrapolatedMappedBasesInQ7Alignments;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q7_longest_alignment\"")
    private int extrapolatedQ7LongestAlignment;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_50q7_reads\"")
    private int extrapolated50q7Reads;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_100q7_reads\"")
    private int extrapolated100q7Reads;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_200q7_reads\"")
    private int extrapolated200q7Reads;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q10_coverage_percentage\"")
    private double extrapolatedQ10CoveragePercentage;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q10_mean_coverage_depth\"")
    private double extrapolatedQ10MeanCoverageDepth;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q10_alignments\"")
    private int extrapolatedQ10Alignments;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q10_mean_alignment_length\"")
    private int extrapolatedQ10MeanAlignmentLength;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_mapped_bases_in_q10_alignments\"")
    private int extrapolatedMappedBasesInQ10Alignments;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q10_longest_alignment\"")
    private int extrapolatedQ10LongestAlignment;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_50q10_reads\"")
    private int extrapolated50q10Reads;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_100q10_reads\"")
    private int extrapolated100q10Reads;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_200q10_reads\"")
    private int extrapolated200q10Reads;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q17_coverage_percentage\"")
    private double extrapolatedQ17CoveragePercentage;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q17_mean_coverage_depth\"")
    private double extrapolatedQ17MeanCoverageDepth;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q17_alignments\"")
    private int extrapolatedQ17Alignments;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q17_mean_alignment_length\"")
    private int extrapolatedQ17MeanAlignmentLength;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_mapped_bases_in_q17_alignments\"")
    private int extrapolatedMappedBasesInQ17Alignments;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q17_longest_alignment\"")
    private int extrapolatedQ17LongestAlignment;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_50q17_reads\"")
    private int extrapolated50q17Reads;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_100q17_reads\"")
    private int extrapolated100q17Reads;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_200q17_reads\"")
    private int extrapolated200q17Reads;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q20_coverage_percentage\"")
    private double extrapolatedQ20CoveragePercentage;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q20_mean_coverage_depth\"")
    private double extrapolatedQ20MeanCoverageDepth;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q20_alignments\"")
    private int extrapolatedQ20Alignments;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q20_mean_alignment_length\"")
    private int extrapolatedQ20MeanAlignmentLength;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_mapped_bases_in_q20_alignments\"")
    private int extrapolatedMappedBasesInQ20Alignments;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q20_longest_alignment\"")
    private int extrapolatedQ20LongestAlignment;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_50q20_reads\"")
    private int extrapolated50q20Reads;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_100q20_reads\"")
    private int extrapolated100q20Reads;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_200q20_reads\"")
    private int extrapolated200q20Reads;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q47_coverage_percentage\"")
    private double extrapolatedQ47CoveragePercentage;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q47_mean_coverage_depth\"")
    private double extrapolatedQ47MeanCoverageDepth;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q47_alignments\"")
    private int extrapolatedQ47Alignments;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q47_mean_alignment_length\"")
    private int extrapolatedQ47MeanAlignmentLength;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_mapped_bases_in_q47_alignments\"")
    private int extrapolatedMappedBasesInQ47Alignments;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_q47_longest_alignment\"")
    private int extrapolatedQ47LongestAlignment;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_50q47_reads\"")
    private int extrapolated50q47Reads;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_100q47_reads\"")
    private int extrapolated100q47Reads;
    @Basic(optional = false)
    @Column(name = "\"extrapolated_200q47_reads\"")
    private int extrapolated200q47Reads;
    @JoinColumn(name = "report_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private RundbResults reportId;

    public RundbLibmetrics() {

    }

/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( RundbLibmetrics.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( RundbLibmetrics.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( RundbLibmetrics.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
//  System.out.println("RundbLibmetrics: " + msg);
        //Logger.getLogger( RundbLibmetrics.class.getName()).log(Level.INFO, msg, ex);
    }

    public RundbLibmetrics(Integer id) {
        this.id = id;
    }

    public RundbLibmetrics(Integer id, double sysSNR, double aveKeyCounts, int totalNumReads, int genomelength, int rNumAlignments, int rMeanAlignLen, int rLongestAlign, double rCoverage, int r50Q10, int r100Q10, int r200Q10, int r50Q17, int r100Q17, int r200Q17, int r50Q20, int r100Q20, int r200Q20, int sNumAlignments, int sMeanAlignLen, int sLongestAlign, double sCoverage, int s50Q10, int s100Q10, int s200Q10, int s50Q17, int s100Q17, int s200Q17, int s50Q20, int s100Q20, int s200Q20, double q7CoveragePercentage, int q7Alignments, int q7MappedBases, int q7QscoreBases, int q7MeanAlignmentLength, int q7LongestAlignment, int i50Q7reads, int i100Q7reads, int i200Q7reads, double q10CoveragePercentage, int q10Alignments, int q10MappedBases, int q10QscoreBases, int q10MeanAlignmentLength, int q10LongestAlignment, int i50Q10reads, int i100Q10reads, int i200Q10reads, double q17CoveragePercentage, int q17Alignments, int q17MappedBases, int q17QscoreBases, int q17MeanAlignmentLength, int q17LongestAlignment, int i50Q17reads, int i100Q17reads, int i200Q17reads, double q20CoveragePercentage, int q20Alignments, int q20MappedBases, int q20QscoreBases, int q20MeanAlignmentLength, int q20LongestAlignment, int i50Q20reads, int i100Q20reads, int i200Q20reads, double q47CoveragePercentage, int q47MappedBases, int q47QscoreBases, int q47Alignments, int q47MeanAlignmentLength, int q47LongestAlignment, int i50Q47reads, int i100Q47reads, int i200Q47reads, double cf, double ie, double dr, String genomeVersion, String indexVersion, int alignSample, String genome, long genomesize, int totalNumberOfSampledReads, double sampledQ7CoveragePercentage, double sampledQ7MeanCoverageDepth, int sampledQ7Alignments, int sampledQ7MeanAlignmentLength, int sampledMappedBasesInQ7Alignments, int sampledQ7LongestAlignment, int sampled50q7Reads, int sampled100q7Reads, int sampled200q7Reads, double sampledQ10CoveragePercentage, double sampledQ10MeanCoverageDepth, int sampledQ10Alignments, int sampledQ10MeanAlignmentLength, int sampledMappedBasesInQ10Alignments, int sampledQ10LongestAlignment, int sampled50q10Reads, int sampled100q10Reads, int sampled200q10Reads, double sampledQ17CoveragePercentage, double sampledQ17MeanCoverageDepth, int sampledQ17Alignments, int sampledQ17MeanAlignmentLength, int sampledMappedBasesInQ17Alignments, int sampledQ17LongestAlignment, int sampled50q17Reads, int sampled100q17Reads, int sampled200q17Reads, double sampledQ20CoveragePercentage, double sampledQ20MeanCoverageDepth, int sampledQ20Alignments, int sampledQ20MeanAlignmentLength, int sampledMappedBasesInQ20Alignments, int sampledQ20LongestAlignment, int sampled50q20Reads, int sampled100q20Reads, int sampled200q20Reads, double sampledQ47CoveragePercentage, double sampledQ47MeanCoverageDepth, int sampledQ47Alignments, int sampledQ47MeanAlignmentLength, int sampledMappedBasesInQ47Alignments, int sampledQ47LongestAlignment, int sampled50q47Reads, int sampled100q47Reads, int sampled200q47Reads, int extrapolatedFromNumberOfSampledReads, double extrapolatedQ7CoveragePercentage, double extrapolatedQ7MeanCoverageDepth, int extrapolatedQ7Alignments, int extrapolatedQ7MeanAlignmentLength, int extrapolatedMappedBasesInQ7Alignments, int extrapolatedQ7LongestAlignment, int extrapolated50q7Reads, int extrapolated100q7Reads, int extrapolated200q7Reads, double extrapolatedQ10CoveragePercentage, double extrapolatedQ10MeanCoverageDepth, int extrapolatedQ10Alignments, int extrapolatedQ10MeanAlignmentLength, int extrapolatedMappedBasesInQ10Alignments, int extrapolatedQ10LongestAlignment, int extrapolated50q10Reads, int extrapolated100q10Reads, int extrapolated200q10Reads, double extrapolatedQ17CoveragePercentage, double extrapolatedQ17MeanCoverageDepth, int extrapolatedQ17Alignments, int extrapolatedQ17MeanAlignmentLength, int extrapolatedMappedBasesInQ17Alignments, int extrapolatedQ17LongestAlignment, int extrapolated50q17Reads, int extrapolated100q17Reads, int extrapolated200q17Reads, double extrapolatedQ20CoveragePercentage, double extrapolatedQ20MeanCoverageDepth, int extrapolatedQ20Alignments, int extrapolatedQ20MeanAlignmentLength, int extrapolatedMappedBasesInQ20Alignments, int extrapolatedQ20LongestAlignment, int extrapolated50q20Reads, int extrapolated100q20Reads, int extrapolated200q20Reads, double extrapolatedQ47CoveragePercentage, double extrapolatedQ47MeanCoverageDepth, int extrapolatedQ47Alignments, int extrapolatedQ47MeanAlignmentLength, int extrapolatedMappedBasesInQ47Alignments, int extrapolatedQ47LongestAlignment, int extrapolated50q47Reads, int extrapolated100q47Reads, int extrapolated200q47Reads) {
        this.id = id;
        this.sysSNR = sysSNR;
        this.aveKeyCounts = aveKeyCounts;
        this.totalNumReads = totalNumReads;
        this.genomelength = genomelength;
        this.rNumAlignments = rNumAlignments;
        this.rMeanAlignLen = rMeanAlignLen;
        this.rLongestAlign = rLongestAlign;
        this.rCoverage = rCoverage;
        this.r50Q10 = r50Q10;
        this.r100Q10 = r100Q10;
        this.r200Q10 = r200Q10;
        this.r50Q17 = r50Q17;
        this.r100Q17 = r100Q17;
        this.r200Q17 = r200Q17;
        this.r50Q20 = r50Q20;
        this.r100Q20 = r100Q20;
        this.r200Q20 = r200Q20;
        this.sNumAlignments = sNumAlignments;
        this.sMeanAlignLen = sMeanAlignLen;
        this.sLongestAlign = sLongestAlign;
        this.sCoverage = sCoverage;
        this.s50Q10 = s50Q10;
        this.s100Q10 = s100Q10;
        this.s200Q10 = s200Q10;
        this.s50Q17 = s50Q17;
        this.s100Q17 = s100Q17;
        this.s200Q17 = s200Q17;
        this.s50Q20 = s50Q20;
        this.s100Q20 = s100Q20;
        this.s200Q20 = s200Q20;
        this.q7CoveragePercentage = q7CoveragePercentage;
        this.q7Alignments = q7Alignments;
        this.q7MappedBases = q7MappedBases;
        this.q7QscoreBases = q7QscoreBases;
        this.q7MeanAlignmentLength = q7MeanAlignmentLength;
        this.q7LongestAlignment = q7LongestAlignment;
        this.i50Q7reads = i50Q7reads;
        this.i100Q7reads = i100Q7reads;
        this.i200Q7reads = i200Q7reads;
        this.q10CoveragePercentage = q10CoveragePercentage;
        this.q10Alignments = q10Alignments;
        this.q10MappedBases = q10MappedBases;
        this.q10QscoreBases = q10QscoreBases;
        this.q10MeanAlignmentLength = q10MeanAlignmentLength;
        this.q10LongestAlignment = q10LongestAlignment;
        this.i50Q10reads = i50Q10reads;
        this.i100Q10reads = i100Q10reads;
        this.i200Q10reads = i200Q10reads;
        this.q17CoveragePercentage = q17CoveragePercentage;
        this.q17Alignments = q17Alignments;
        this.q17MappedBases = q17MappedBases;
        this.q17QscoreBases = q17QscoreBases;
        this.q17MeanAlignmentLength = q17MeanAlignmentLength;
        this.q17LongestAlignment = q17LongestAlignment;
        this.i50Q17reads = i50Q17reads;
        this.i100Q17reads = i100Q17reads;
        this.i200Q17reads = i200Q17reads;
        this.q20CoveragePercentage = q20CoveragePercentage;
        this.q20Alignments = q20Alignments;
        this.q20MappedBases = q20MappedBases;
        this.q20QscoreBases = q20QscoreBases;
        this.q20MeanAlignmentLength = q20MeanAlignmentLength;
        this.q20LongestAlignment = q20LongestAlignment;
        this.i50Q20reads = i50Q20reads;
        this.i100Q20reads = i100Q20reads;
        this.i200Q20reads = i200Q20reads;
        this.q47CoveragePercentage = q47CoveragePercentage;
        this.q47MappedBases = q47MappedBases;
        this.q47QscoreBases = q47QscoreBases;
        this.q47Alignments = q47Alignments;
        this.q47MeanAlignmentLength = q47MeanAlignmentLength;
        this.q47LongestAlignment = q47LongestAlignment;
        this.i50Q47reads = i50Q47reads;
        this.i100Q47reads = i100Q47reads;
        this.i200Q47reads = i200Q47reads;
        this.cf = cf;
        this.ie = ie;
        this.dr = dr;
        this.genomeVersion = genomeVersion;
        this.indexVersion = indexVersion;
        this.alignSample = alignSample;
        this.genome = genome;
        this.genomesize = genomesize;
        this.totalNumberOfSampledReads = totalNumberOfSampledReads;
        this.sampledQ7CoveragePercentage = sampledQ7CoveragePercentage;
        this.sampledQ7MeanCoverageDepth = sampledQ7MeanCoverageDepth;
        this.sampledQ7Alignments = sampledQ7Alignments;
        this.sampledQ7MeanAlignmentLength = sampledQ7MeanAlignmentLength;
        this.sampledMappedBasesInQ7Alignments = sampledMappedBasesInQ7Alignments;
        this.sampledQ7LongestAlignment = sampledQ7LongestAlignment;
        this.sampled50q7Reads = sampled50q7Reads;
        this.sampled100q7Reads = sampled100q7Reads;
        this.sampled200q7Reads = sampled200q7Reads;
        this.sampledQ10CoveragePercentage = sampledQ10CoveragePercentage;
        this.sampledQ10MeanCoverageDepth = sampledQ10MeanCoverageDepth;
        this.sampledQ10Alignments = sampledQ10Alignments;
        this.sampledQ10MeanAlignmentLength = sampledQ10MeanAlignmentLength;
        this.sampledMappedBasesInQ10Alignments = sampledMappedBasesInQ10Alignments;
        this.sampledQ10LongestAlignment = sampledQ10LongestAlignment;
        this.sampled50q10Reads = sampled50q10Reads;
        this.sampled100q10Reads = sampled100q10Reads;
        this.sampled200q10Reads = sampled200q10Reads;
        this.sampledQ17CoveragePercentage = sampledQ17CoveragePercentage;
        this.sampledQ17MeanCoverageDepth = sampledQ17MeanCoverageDepth;
        this.sampledQ17Alignments = sampledQ17Alignments;
        this.sampledQ17MeanAlignmentLength = sampledQ17MeanAlignmentLength;
        this.sampledMappedBasesInQ17Alignments = sampledMappedBasesInQ17Alignments;
        this.sampledQ17LongestAlignment = sampledQ17LongestAlignment;
        this.sampled50q17Reads = sampled50q17Reads;
        this.sampled100q17Reads = sampled100q17Reads;
        this.sampled200q17Reads = sampled200q17Reads;
        this.sampledQ20CoveragePercentage = sampledQ20CoveragePercentage;
        this.sampledQ20MeanCoverageDepth = sampledQ20MeanCoverageDepth;
        this.sampledQ20Alignments = sampledQ20Alignments;
        this.sampledQ20MeanAlignmentLength = sampledQ20MeanAlignmentLength;
        this.sampledMappedBasesInQ20Alignments = sampledMappedBasesInQ20Alignments;
        this.sampledQ20LongestAlignment = sampledQ20LongestAlignment;
        this.sampled50q20Reads = sampled50q20Reads;
        this.sampled100q20Reads = sampled100q20Reads;
        this.sampled200q20Reads = sampled200q20Reads;
        this.sampledQ47CoveragePercentage = sampledQ47CoveragePercentage;
        this.sampledQ47MeanCoverageDepth = sampledQ47MeanCoverageDepth;
        this.sampledQ47Alignments = sampledQ47Alignments;
        this.sampledQ47MeanAlignmentLength = sampledQ47MeanAlignmentLength;
        this.sampledMappedBasesInQ47Alignments = sampledMappedBasesInQ47Alignments;
        this.sampledQ47LongestAlignment = sampledQ47LongestAlignment;
        this.sampled50q47Reads = sampled50q47Reads;
        this.sampled100q47Reads = sampled100q47Reads;
        this.sampled200q47Reads = sampled200q47Reads;
        this.extrapolatedFromNumberOfSampledReads = extrapolatedFromNumberOfSampledReads;
        this.extrapolatedQ7CoveragePercentage = extrapolatedQ7CoveragePercentage;
        this.extrapolatedQ7MeanCoverageDepth = extrapolatedQ7MeanCoverageDepth;
        this.extrapolatedQ7Alignments = extrapolatedQ7Alignments;
        this.extrapolatedQ7MeanAlignmentLength = extrapolatedQ7MeanAlignmentLength;
        this.extrapolatedMappedBasesInQ7Alignments = extrapolatedMappedBasesInQ7Alignments;
        this.extrapolatedQ7LongestAlignment = extrapolatedQ7LongestAlignment;
        this.extrapolated50q7Reads = extrapolated50q7Reads;
        this.extrapolated100q7Reads = extrapolated100q7Reads;
        this.extrapolated200q7Reads = extrapolated200q7Reads;
        this.extrapolatedQ10CoveragePercentage = extrapolatedQ10CoveragePercentage;
        this.extrapolatedQ10MeanCoverageDepth = extrapolatedQ10MeanCoverageDepth;
        this.extrapolatedQ10Alignments = extrapolatedQ10Alignments;
        this.extrapolatedQ10MeanAlignmentLength = extrapolatedQ10MeanAlignmentLength;
        this.extrapolatedMappedBasesInQ10Alignments = extrapolatedMappedBasesInQ10Alignments;
        this.extrapolatedQ10LongestAlignment = extrapolatedQ10LongestAlignment;
        this.extrapolated50q10Reads = extrapolated50q10Reads;
        this.extrapolated100q10Reads = extrapolated100q10Reads;
        this.extrapolated200q10Reads = extrapolated200q10Reads;
        this.extrapolatedQ17CoveragePercentage = extrapolatedQ17CoveragePercentage;
        this.extrapolatedQ17MeanCoverageDepth = extrapolatedQ17MeanCoverageDepth;
        this.extrapolatedQ17Alignments = extrapolatedQ17Alignments;
        this.extrapolatedQ17MeanAlignmentLength = extrapolatedQ17MeanAlignmentLength;
        this.extrapolatedMappedBasesInQ17Alignments = extrapolatedMappedBasesInQ17Alignments;
        this.extrapolatedQ17LongestAlignment = extrapolatedQ17LongestAlignment;
        this.extrapolated50q17Reads = extrapolated50q17Reads;
        this.extrapolated100q17Reads = extrapolated100q17Reads;
        this.extrapolated200q17Reads = extrapolated200q17Reads;
        this.extrapolatedQ20CoveragePercentage = extrapolatedQ20CoveragePercentage;
        this.extrapolatedQ20MeanCoverageDepth = extrapolatedQ20MeanCoverageDepth;
        this.extrapolatedQ20Alignments = extrapolatedQ20Alignments;
        this.extrapolatedQ20MeanAlignmentLength = extrapolatedQ20MeanAlignmentLength;
        this.extrapolatedMappedBasesInQ20Alignments = extrapolatedMappedBasesInQ20Alignments;
        this.extrapolatedQ20LongestAlignment = extrapolatedQ20LongestAlignment;
        this.extrapolated50q20Reads = extrapolated50q20Reads;
        this.extrapolated100q20Reads = extrapolated100q20Reads;
        this.extrapolated200q20Reads = extrapolated200q20Reads;
        this.extrapolatedQ47CoveragePercentage = extrapolatedQ47CoveragePercentage;
        this.extrapolatedQ47MeanCoverageDepth = extrapolatedQ47MeanCoverageDepth;
        this.extrapolatedQ47Alignments = extrapolatedQ47Alignments;
        this.extrapolatedQ47MeanAlignmentLength = extrapolatedQ47MeanAlignmentLength;
        this.extrapolatedMappedBasesInQ47Alignments = extrapolatedMappedBasesInQ47Alignments;
        this.extrapolatedQ47LongestAlignment = extrapolatedQ47LongestAlignment;
        this.extrapolated50q47Reads = extrapolated50q47Reads;
        this.extrapolated100q47Reads = extrapolated100q47Reads;
        this.extrapolated200q47Reads = extrapolated200q47Reads;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public double getSysSNR() {
        return sysSNR;
    }

    public void setSysSNR(double sysSNR) {
        this.sysSNR = sysSNR;
    }

    public double getAveKeyCounts() {
        return aveKeyCounts;
    }

    public void setAveKeyCounts(double aveKeyCounts) {
        this.aveKeyCounts = aveKeyCounts;
    }

    public int getTotalNumReads() {
        return totalNumReads;
    }

    public void setTotalNumReads(int totalNumReads) {
        this.totalNumReads = totalNumReads;
    }

    public int getGenomelength() {
        return genomelength;
    }

    public void setGenomelength(int genomelength) {
        this.genomelength = genomelength;
    }

    public int getRNumAlignments() {
        return rNumAlignments;
    }

    public void setRNumAlignments(int rNumAlignments) {
        this.rNumAlignments = rNumAlignments;
    }

    public int getRMeanAlignLen() {
        return rMeanAlignLen;
    }

    public void setRMeanAlignLen(int rMeanAlignLen) {
        this.rMeanAlignLen = rMeanAlignLen;
    }

    public int getRLongestAlign() {
        return rLongestAlign;
    }

    public void setRLongestAlign(int rLongestAlign) {
        this.rLongestAlign = rLongestAlign;
    }

    public double getRCoverage() {
        return rCoverage;
    }

    public void setRCoverage(double rCoverage) {
        this.rCoverage = rCoverage;
    }

    public int getR50Q10() {
        return r50Q10;
    }

    public void setR50Q10(int r50Q10) {
        this.r50Q10 = r50Q10;
    }

    public int getR100Q10() {
        return r100Q10;
    }

    public void setR100Q10(int r100Q10) {
        this.r100Q10 = r100Q10;
    }

    public int getR200Q10() {
        return r200Q10;
    }

    public void setR200Q10(int r200Q10) {
        this.r200Q10 = r200Q10;
    }

    public int getR50Q17() {
        return r50Q17;
    }

    public void setR50Q17(int r50Q17) {
        this.r50Q17 = r50Q17;
    }

    public int getR100Q17() {
        return r100Q17;
    }

    public void setR100Q17(int r100Q17) {
        this.r100Q17 = r100Q17;
    }

    public int getR200Q17() {
        return r200Q17;
    }

    public void setR200Q17(int r200Q17) {
        this.r200Q17 = r200Q17;
    }

    public int getR50Q20() {
        return r50Q20;
    }

    public void setR50Q20(int r50Q20) {
        this.r50Q20 = r50Q20;
    }

    public int getR100Q20() {
        return r100Q20;
    }

    public void setR100Q20(int r100Q20) {
        this.r100Q20 = r100Q20;
    }

    public int getR200Q20() {
        return r200Q20;
    }

    public void setR200Q20(int r200Q20) {
        this.r200Q20 = r200Q20;
    }

    public int getSNumAlignments() {
        return sNumAlignments;
    }

    public void setSNumAlignments(int sNumAlignments) {
        this.sNumAlignments = sNumAlignments;
    }

    public int getSMeanAlignLen() {
        return sMeanAlignLen;
    }

    public void setSMeanAlignLen(int sMeanAlignLen) {
        this.sMeanAlignLen = sMeanAlignLen;
    }

    public int getSLongestAlign() {
        return sLongestAlign;
    }

    public void setSLongestAlign(int sLongestAlign) {
        this.sLongestAlign = sLongestAlign;
    }

    public double getSCoverage() {
        return sCoverage;
    }

    public void setSCoverage(double sCoverage) {
        this.sCoverage = sCoverage;
    }

    public int getS50Q10() {
        return s50Q10;
    }

    public void setS50Q10(int s50Q10) {
        this.s50Q10 = s50Q10;
    }

    public int getS100Q10() {
        return s100Q10;
    }

    public void setS100Q10(int s100Q10) {
        this.s100Q10 = s100Q10;
    }

    public int getS200Q10() {
        return s200Q10;
    }

    public void setS200Q10(int s200Q10) {
        this.s200Q10 = s200Q10;
    }

    public int getS50Q17() {
        return s50Q17;
    }

    public void setS50Q17(int s50Q17) {
        this.s50Q17 = s50Q17;
    }

    public int getS100Q17() {
        return s100Q17;
    }

    public void setS100Q17(int s100Q17) {
        this.s100Q17 = s100Q17;
    }

    public int getS200Q17() {
        return s200Q17;
    }

    public void setS200Q17(int s200Q17) {
        this.s200Q17 = s200Q17;
    }

    public int getS50Q20() {
        return s50Q20;
    }

    public void setS50Q20(int s50Q20) {
        this.s50Q20 = s50Q20;
    }

    public int getS100Q20() {
        return s100Q20;
    }

    public void setS100Q20(int s100Q20) {
        this.s100Q20 = s100Q20;
    }

    public int getS200Q20() {
        return s200Q20;
    }

    public void setS200Q20(int s200Q20) {
        this.s200Q20 = s200Q20;
    }

    public double getQ7CoveragePercentage() {
        return q7CoveragePercentage;
    }

    public void setQ7CoveragePercentage(double q7CoveragePercentage) {
        this.q7CoveragePercentage = q7CoveragePercentage;
    }

    public int getQ7Alignments() {
        return q7Alignments;
    }

    public void setQ7Alignments(int q7Alignments) {
        this.q7Alignments = q7Alignments;
    }

    public int getQ7MappedBases() {
        return q7MappedBases;
    }

    public void setQ7MappedBases(int q7MappedBases) {
        this.q7MappedBases = q7MappedBases;
    }

    public int getQ7QscoreBases() {
        return q7QscoreBases;
    }

    public void setQ7QscoreBases(int q7QscoreBases) {
        this.q7QscoreBases = q7QscoreBases;
    }

    public int getQ7MeanAlignmentLength() {
        return q7MeanAlignmentLength;
    }

    public void setQ7MeanAlignmentLength(int q7MeanAlignmentLength) {
        this.q7MeanAlignmentLength = q7MeanAlignmentLength;
    }

    public int getQ7LongestAlignment() {
        return q7LongestAlignment;
    }

    public void setQ7LongestAlignment(int q7LongestAlignment) {
        this.q7LongestAlignment = q7LongestAlignment;
    }

    public int getI50Q7reads() {
        return i50Q7reads;
    }

    public void setI50Q7reads(int i50Q7reads) {
        this.i50Q7reads = i50Q7reads;
    }

    public int getI100Q7reads() {
        return i100Q7reads;
    }

    public void setI100Q7reads(int i100Q7reads) {
        this.i100Q7reads = i100Q7reads;
    }

    public int getI200Q7reads() {
        return i200Q7reads;
    }

    public void setI200Q7reads(int i200Q7reads) {
        this.i200Q7reads = i200Q7reads;
    }

    public double getQ10CoveragePercentage() {
        return q10CoveragePercentage;
    }

    public void setQ10CoveragePercentage(double q10CoveragePercentage) {
        this.q10CoveragePercentage = q10CoveragePercentage;
    }

    public int getQ10Alignments() {
        return q10Alignments;
    }

    public void setQ10Alignments(int q10Alignments) {
        this.q10Alignments = q10Alignments;
    }

    public int getQ10MappedBases() {
        return q10MappedBases;
    }

    public void setQ10MappedBases(int q10MappedBases) {
        this.q10MappedBases = q10MappedBases;
    }

    public int getQ10QscoreBases() {
        return q10QscoreBases;
    }

    public void setQ10QscoreBases(int q10QscoreBases) {
        this.q10QscoreBases = q10QscoreBases;
    }

    public int getQ10MeanAlignmentLength() {
        return q10MeanAlignmentLength;
    }

    public void setQ10MeanAlignmentLength(int q10MeanAlignmentLength) {
        this.q10MeanAlignmentLength = q10MeanAlignmentLength;
    }

    public int getQ10LongestAlignment() {
        return q10LongestAlignment;
    }

    public void setQ10LongestAlignment(int q10LongestAlignment) {
        this.q10LongestAlignment = q10LongestAlignment;
    }

    public int getI50Q10reads() {
        return i50Q10reads;
    }

    public void setI50Q10reads(int i50Q10reads) {
        this.i50Q10reads = i50Q10reads;
    }

    public int getI100Q10reads() {
        return i100Q10reads;
    }

    public void setI100Q10reads(int i100Q10reads) {
        this.i100Q10reads = i100Q10reads;
    }

    public int getI200Q10reads() {
        return i200Q10reads;
    }

    public void setI200Q10reads(int i200Q10reads) {
        this.i200Q10reads = i200Q10reads;
    }

    public double getQ17CoveragePercentage() {
        return q17CoveragePercentage;
    }

    public void setQ17CoveragePercentage(double q17CoveragePercentage) {
        this.q17CoveragePercentage = q17CoveragePercentage;
    }

    public int getQ17Alignments() {
        return q17Alignments;
    }

    public void setQ17Alignments(int q17Alignments) {
        this.q17Alignments = q17Alignments;
    }

    public int getQ17MappedBases() {
        return q17MappedBases;
    }

    public void setQ17MappedBases(int q17MappedBases) {
        this.q17MappedBases = q17MappedBases;
    }

    public int getQ17QscoreBases() {
        return q17QscoreBases;
    }

    public void setQ17QscoreBases(int q17QscoreBases) {
        this.q17QscoreBases = q17QscoreBases;
    }

    public int getQ17MeanAlignmentLength() {
        return q17MeanAlignmentLength;
    }

    public void setQ17MeanAlignmentLength(int q17MeanAlignmentLength) {
        this.q17MeanAlignmentLength = q17MeanAlignmentLength;
    }

    public int getQ17LongestAlignment() {
        return q17LongestAlignment;
    }

    public void setQ17LongestAlignment(int q17LongestAlignment) {
        this.q17LongestAlignment = q17LongestAlignment;
    }

    public int getI50Q17reads() {
        return i50Q17reads;
    }

    public void setI50Q17reads(int i50Q17reads) {
        this.i50Q17reads = i50Q17reads;
    }

    public int getI100Q17reads() {
        return i100Q17reads;
    }

    public void setI100Q17reads(int i100Q17reads) {
        this.i100Q17reads = i100Q17reads;
    }

    public int getI200Q17reads() {
        return i200Q17reads;
    }

    public void setI200Q17reads(int i200Q17reads) {
        this.i200Q17reads = i200Q17reads;
    }

    public double getQ20CoveragePercentage() {
        return q20CoveragePercentage;
    }

    public void setQ20CoveragePercentage(double q20CoveragePercentage) {
        this.q20CoveragePercentage = q20CoveragePercentage;
    }

    public int getQ20Alignments() {
        return q20Alignments;
    }

    public void setQ20Alignments(int q20Alignments) {
        this.q20Alignments = q20Alignments;
    }

    public int getQ20MappedBases() {
        return q20MappedBases;
    }

    public void setQ20MappedBases(int q20MappedBases) {
        this.q20MappedBases = q20MappedBases;
    }

    public int getQ20QscoreBases() {
        return q20QscoreBases;
    }

    public void setQ20QscoreBases(int q20QscoreBases) {
        this.q20QscoreBases = q20QscoreBases;
    }

    public int getQ20MeanAlignmentLength() {
        return q20MeanAlignmentLength;
    }

    public void setQ20MeanAlignmentLength(int q20MeanAlignmentLength) {
        this.q20MeanAlignmentLength = q20MeanAlignmentLength;
    }

    public int getQ20LongestAlignment() {
        return q20LongestAlignment;
    }

    public void setQ20LongestAlignment(int q20LongestAlignment) {
        this.q20LongestAlignment = q20LongestAlignment;
    }

    public int getI50Q20reads() {
        return i50Q20reads;
    }

    public void setI50Q20reads(int i50Q20reads) {
        this.i50Q20reads = i50Q20reads;
    }

    public int getI100Q20reads() {
        return i100Q20reads;
    }

    public void setI100Q20reads(int i100Q20reads) {
        this.i100Q20reads = i100Q20reads;
    }

    public int getI200Q20reads() {
        return i200Q20reads;
    }

    public void setI200Q20reads(int i200Q20reads) {
        this.i200Q20reads = i200Q20reads;
    }

    public double getQ47CoveragePercentage() {
        return q47CoveragePercentage;
    }

    public void setQ47CoveragePercentage(double q47CoveragePercentage) {
        this.q47CoveragePercentage = q47CoveragePercentage;
    }

    public int getQ47MappedBases() {
        return q47MappedBases;
    }

    public void setQ47MappedBases(int q47MappedBases) {
        this.q47MappedBases = q47MappedBases;
    }

    public int getQ47QscoreBases() {
        return q47QscoreBases;
    }

    public void setQ47QscoreBases(int q47QscoreBases) {
        this.q47QscoreBases = q47QscoreBases;
    }

    public int getQ47Alignments() {
        return q47Alignments;
    }

    public void setQ47Alignments(int q47Alignments) {
        this.q47Alignments = q47Alignments;
    }

    public int getQ47MeanAlignmentLength() {
        return q47MeanAlignmentLength;
    }

    public void setQ47MeanAlignmentLength(int q47MeanAlignmentLength) {
        this.q47MeanAlignmentLength = q47MeanAlignmentLength;
    }

    public int getQ47LongestAlignment() {
        return q47LongestAlignment;
    }

    public void setQ47LongestAlignment(int q47LongestAlignment) {
        this.q47LongestAlignment = q47LongestAlignment;
    }

    public int getI50Q47reads() {
        return i50Q47reads;
    }

    public void setI50Q47reads(int i50Q47reads) {
        this.i50Q47reads = i50Q47reads;
    }

    public int getI100Q47reads() {
        return i100Q47reads;
    }

    public void setI100Q47reads(int i100Q47reads) {
        this.i100Q47reads = i100Q47reads;
    }

    public int getI200Q47reads() {
        return i200Q47reads;
    }

    public void setI200Q47reads(int i200Q47reads) {
        this.i200Q47reads = i200Q47reads;
    }

    public double getCf() {
        return cf;
    }

    public void setCf(double cf) {
        this.cf = cf;
    }

    public double getIe() {
        return ie;
    }

    public void setIe(double ie) {
        this.ie = ie;
    }

    public double getDr() {
        return dr;
    }

    public void setDr(double dr) {
        this.dr = dr;
    }

    public String getGenomeVersion() {
        return genomeVersion;
    }

    public void setGenomeVersion(String genomeVersion) {
        this.genomeVersion = genomeVersion;
    }

    public String getIndexVersion() {
        return indexVersion;
    }

    public void setIndexVersion(String indexVersion) {
        this.indexVersion = indexVersion;
    }

    public int getAlignSample() {
        return alignSample;
    }

    public void setAlignSample(int alignSample) {
        this.alignSample = alignSample;
    }

    public String getGenome() {
        return genome;
    }

    public void setGenome(String genome) {
        this.genome = genome;
    }

    public long getGenomesize() {
        return genomesize;
    }

    public void setGenomesize(long genomesize) {
        this.genomesize = genomesize;
    }

    public int getTotalNumberOfSampledReads() {
        return totalNumberOfSampledReads;
    }

    public void setTotalNumberOfSampledReads(int totalNumberOfSampledReads) {
        this.totalNumberOfSampledReads = totalNumberOfSampledReads;
    }

    public double getSampledQ7CoveragePercentage() {
        return sampledQ7CoveragePercentage;
    }

    public void setSampledQ7CoveragePercentage(double sampledQ7CoveragePercentage) {
        this.sampledQ7CoveragePercentage = sampledQ7CoveragePercentage;
    }

    public double getSampledQ7MeanCoverageDepth() {
        return sampledQ7MeanCoverageDepth;
    }

    public void setSampledQ7MeanCoverageDepth(double sampledQ7MeanCoverageDepth) {
        this.sampledQ7MeanCoverageDepth = sampledQ7MeanCoverageDepth;
    }

    public int getSampledQ7Alignments() {
        return sampledQ7Alignments;
    }

    public void setSampledQ7Alignments(int sampledQ7Alignments) {
        this.sampledQ7Alignments = sampledQ7Alignments;
    }

    public int getSampledQ7MeanAlignmentLength() {
        return sampledQ7MeanAlignmentLength;
    }

    public void setSampledQ7MeanAlignmentLength(int sampledQ7MeanAlignmentLength) {
        this.sampledQ7MeanAlignmentLength = sampledQ7MeanAlignmentLength;
    }

    public int getSampledMappedBasesInQ7Alignments() {
        return sampledMappedBasesInQ7Alignments;
    }

    public void setSampledMappedBasesInQ7Alignments(int sampledMappedBasesInQ7Alignments) {
        this.sampledMappedBasesInQ7Alignments = sampledMappedBasesInQ7Alignments;
    }

    public int getSampledQ7LongestAlignment() {
        return sampledQ7LongestAlignment;
    }

    public void setSampledQ7LongestAlignment(int sampledQ7LongestAlignment) {
        this.sampledQ7LongestAlignment = sampledQ7LongestAlignment;
    }

    public int getSampled50q7Reads() {
        return sampled50q7Reads;
    }

    public void setSampled50q7Reads(int sampled50q7Reads) {
        this.sampled50q7Reads = sampled50q7Reads;
    }

    public int getSampled100q7Reads() {
        return sampled100q7Reads;
    }

    public void setSampled100q7Reads(int sampled100q7Reads) {
        this.sampled100q7Reads = sampled100q7Reads;
    }

    public int getSampled200q7Reads() {
        return sampled200q7Reads;
    }

    public void setSampled200q7Reads(int sampled200q7Reads) {
        this.sampled200q7Reads = sampled200q7Reads;
    }

    public double getSampledQ10CoveragePercentage() {
        return sampledQ10CoveragePercentage;
    }

    public void setSampledQ10CoveragePercentage(double sampledQ10CoveragePercentage) {
        this.sampledQ10CoveragePercentage = sampledQ10CoveragePercentage;
    }

    public double getSampledQ10MeanCoverageDepth() {
        return sampledQ10MeanCoverageDepth;
    }

    public void setSampledQ10MeanCoverageDepth(double sampledQ10MeanCoverageDepth) {
        this.sampledQ10MeanCoverageDepth = sampledQ10MeanCoverageDepth;
    }

    public int getSampledQ10Alignments() {
        return sampledQ10Alignments;
    }

    public void setSampledQ10Alignments(int sampledQ10Alignments) {
        this.sampledQ10Alignments = sampledQ10Alignments;
    }

    public int getSampledQ10MeanAlignmentLength() {
        return sampledQ10MeanAlignmentLength;
    }

    public void setSampledQ10MeanAlignmentLength(int sampledQ10MeanAlignmentLength) {
        this.sampledQ10MeanAlignmentLength = sampledQ10MeanAlignmentLength;
    }

    public int getSampledMappedBasesInQ10Alignments() {
        return sampledMappedBasesInQ10Alignments;
    }

    public void setSampledMappedBasesInQ10Alignments(int sampledMappedBasesInQ10Alignments) {
        this.sampledMappedBasesInQ10Alignments = sampledMappedBasesInQ10Alignments;
    }

    public int getSampledQ10LongestAlignment() {
        return sampledQ10LongestAlignment;
    }

    public void setSampledQ10LongestAlignment(int sampledQ10LongestAlignment) {
        this.sampledQ10LongestAlignment = sampledQ10LongestAlignment;
    }

    public int getSampled50q10Reads() {
        return sampled50q10Reads;
    }

    public void setSampled50q10Reads(int sampled50q10Reads) {
        this.sampled50q10Reads = sampled50q10Reads;
    }

    public int getSampled100q10Reads() {
        return sampled100q10Reads;
    }

    public void setSampled100q10Reads(int sampled100q10Reads) {
        this.sampled100q10Reads = sampled100q10Reads;
    }

    public int getSampled200q10Reads() {
        return sampled200q10Reads;
    }

    public void setSampled200q10Reads(int sampled200q10Reads) {
        this.sampled200q10Reads = sampled200q10Reads;
    }

    public double getSampledQ17CoveragePercentage() {
        return sampledQ17CoveragePercentage;
    }

    public void setSampledQ17CoveragePercentage(double sampledQ17CoveragePercentage) {
        this.sampledQ17CoveragePercentage = sampledQ17CoveragePercentage;
    }

    public double getSampledQ17MeanCoverageDepth() {
        return sampledQ17MeanCoverageDepth;
    }

    public void setSampledQ17MeanCoverageDepth(double sampledQ17MeanCoverageDepth) {
        this.sampledQ17MeanCoverageDepth = sampledQ17MeanCoverageDepth;
    }

    public int getSampledQ17Alignments() {
        return sampledQ17Alignments;
    }

    public void setSampledQ17Alignments(int sampledQ17Alignments) {
        this.sampledQ17Alignments = sampledQ17Alignments;
    }

    public int getSampledQ17MeanAlignmentLength() {
        return sampledQ17MeanAlignmentLength;
    }

    public void setSampledQ17MeanAlignmentLength(int sampledQ17MeanAlignmentLength) {
        this.sampledQ17MeanAlignmentLength = sampledQ17MeanAlignmentLength;
    }

    public int getSampledMappedBasesInQ17Alignments() {
        return sampledMappedBasesInQ17Alignments;
    }

    public void setSampledMappedBasesInQ17Alignments(int sampledMappedBasesInQ17Alignments) {
        this.sampledMappedBasesInQ17Alignments = sampledMappedBasesInQ17Alignments;
    }

    public int getSampledQ17LongestAlignment() {
        return sampledQ17LongestAlignment;
    }

    public void setSampledQ17LongestAlignment(int sampledQ17LongestAlignment) {
        this.sampledQ17LongestAlignment = sampledQ17LongestAlignment;
    }

    public int getSampled50q17Reads() {
        return sampled50q17Reads;
    }

    public void setSampled50q17Reads(int sampled50q17Reads) {
        this.sampled50q17Reads = sampled50q17Reads;
    }

    public int getSampled100q17Reads() {
        return sampled100q17Reads;
    }

    public void setSampled100q17Reads(int sampled100q17Reads) {
        this.sampled100q17Reads = sampled100q17Reads;
    }

    public int getSampled200q17Reads() {
        return sampled200q17Reads;
    }

    public void setSampled200q17Reads(int sampled200q17Reads) {
        this.sampled200q17Reads = sampled200q17Reads;
    }

    public double getSampledQ20CoveragePercentage() {
        return sampledQ20CoveragePercentage;
    }

    public void setSampledQ20CoveragePercentage(double sampledQ20CoveragePercentage) {
        this.sampledQ20CoveragePercentage = sampledQ20CoveragePercentage;
    }

    public double getSampledQ20MeanCoverageDepth() {
        return sampledQ20MeanCoverageDepth;
    }

    public void setSampledQ20MeanCoverageDepth(double sampledQ20MeanCoverageDepth) {
        this.sampledQ20MeanCoverageDepth = sampledQ20MeanCoverageDepth;
    }

    public int getSampledQ20Alignments() {
        return sampledQ20Alignments;
    }

    public void setSampledQ20Alignments(int sampledQ20Alignments) {
        this.sampledQ20Alignments = sampledQ20Alignments;
    }

    public int getSampledQ20MeanAlignmentLength() {
        return sampledQ20MeanAlignmentLength;
    }

    public void setSampledQ20MeanAlignmentLength(int sampledQ20MeanAlignmentLength) {
        this.sampledQ20MeanAlignmentLength = sampledQ20MeanAlignmentLength;
    }

    public int getSampledMappedBasesInQ20Alignments() {
        return sampledMappedBasesInQ20Alignments;
    }

    public void setSampledMappedBasesInQ20Alignments(int sampledMappedBasesInQ20Alignments) {
        this.sampledMappedBasesInQ20Alignments = sampledMappedBasesInQ20Alignments;
    }

    public int getSampledQ20LongestAlignment() {
        return sampledQ20LongestAlignment;
    }

    public void setSampledQ20LongestAlignment(int sampledQ20LongestAlignment) {
        this.sampledQ20LongestAlignment = sampledQ20LongestAlignment;
    }

    public int getSampled50q20Reads() {
        return sampled50q20Reads;
    }

    public void setSampled50q20Reads(int sampled50q20Reads) {
        this.sampled50q20Reads = sampled50q20Reads;
    }

    public int getSampled100q20Reads() {
        return sampled100q20Reads;
    }

    public void setSampled100q20Reads(int sampled100q20Reads) {
        this.sampled100q20Reads = sampled100q20Reads;
    }

    public int getSampled200q20Reads() {
        return sampled200q20Reads;
    }

    public void setSampled200q20Reads(int sampled200q20Reads) {
        this.sampled200q20Reads = sampled200q20Reads;
    }

    public double getSampledQ47CoveragePercentage() {
        return sampledQ47CoveragePercentage;
    }

    public void setSampledQ47CoveragePercentage(double sampledQ47CoveragePercentage) {
        this.sampledQ47CoveragePercentage = sampledQ47CoveragePercentage;
    }

    public double getSampledQ47MeanCoverageDepth() {
        return sampledQ47MeanCoverageDepth;
    }

    public void setSampledQ47MeanCoverageDepth(double sampledQ47MeanCoverageDepth) {
        this.sampledQ47MeanCoverageDepth = sampledQ47MeanCoverageDepth;
    }

    public int getSampledQ47Alignments() {
        return sampledQ47Alignments;
    }

    public void setSampledQ47Alignments(int sampledQ47Alignments) {
        this.sampledQ47Alignments = sampledQ47Alignments;
    }

    public int getSampledQ47MeanAlignmentLength() {
        return sampledQ47MeanAlignmentLength;
    }

    public void setSampledQ47MeanAlignmentLength(int sampledQ47MeanAlignmentLength) {
        this.sampledQ47MeanAlignmentLength = sampledQ47MeanAlignmentLength;
    }

    public int getSampledMappedBasesInQ47Alignments() {
        return sampledMappedBasesInQ47Alignments;
    }

    public void setSampledMappedBasesInQ47Alignments(int sampledMappedBasesInQ47Alignments) {
        this.sampledMappedBasesInQ47Alignments = sampledMappedBasesInQ47Alignments;
    }

    public int getSampledQ47LongestAlignment() {
        return sampledQ47LongestAlignment;
    }

    public void setSampledQ47LongestAlignment(int sampledQ47LongestAlignment) {
        this.sampledQ47LongestAlignment = sampledQ47LongestAlignment;
    }

    public int getSampled50q47Reads() {
        return sampled50q47Reads;
    }

    public void setSampled50q47Reads(int sampled50q47Reads) {
        this.sampled50q47Reads = sampled50q47Reads;
    }

    public int getSampled100q47Reads() {
        return sampled100q47Reads;
    }

    public void setSampled100q47Reads(int sampled100q47Reads) {
        this.sampled100q47Reads = sampled100q47Reads;
    }

    public int getSampled200q47Reads() {
        return sampled200q47Reads;
    }

    public void setSampled200q47Reads(int sampled200q47Reads) {
        this.sampled200q47Reads = sampled200q47Reads;
    }

    public int getExtrapolatedFromNumberOfSampledReads() {
        return extrapolatedFromNumberOfSampledReads;
    }

    public void setExtrapolatedFromNumberOfSampledReads(int extrapolatedFromNumberOfSampledReads) {
        this.extrapolatedFromNumberOfSampledReads = extrapolatedFromNumberOfSampledReads;
    }

    public double getExtrapolatedQ7CoveragePercentage() {
        return extrapolatedQ7CoveragePercentage;
    }

    public void setExtrapolatedQ7CoveragePercentage(double extrapolatedQ7CoveragePercentage) {
        this.extrapolatedQ7CoveragePercentage = extrapolatedQ7CoveragePercentage;
    }

    public double getExtrapolatedQ7MeanCoverageDepth() {
        return extrapolatedQ7MeanCoverageDepth;
    }

    public void setExtrapolatedQ7MeanCoverageDepth(double extrapolatedQ7MeanCoverageDepth) {
        this.extrapolatedQ7MeanCoverageDepth = extrapolatedQ7MeanCoverageDepth;
    }

    public int getExtrapolatedQ7Alignments() {
        return extrapolatedQ7Alignments;
    }

    public void setExtrapolatedQ7Alignments(int extrapolatedQ7Alignments) {
        this.extrapolatedQ7Alignments = extrapolatedQ7Alignments;
    }

    public int getExtrapolatedQ7MeanAlignmentLength() {
        return extrapolatedQ7MeanAlignmentLength;
    }

    public void setExtrapolatedQ7MeanAlignmentLength(int extrapolatedQ7MeanAlignmentLength) {
        this.extrapolatedQ7MeanAlignmentLength = extrapolatedQ7MeanAlignmentLength;
    }

    public int getExtrapolatedMappedBasesInQ7Alignments() {
        return extrapolatedMappedBasesInQ7Alignments;
    }

    public void setExtrapolatedMappedBasesInQ7Alignments(int extrapolatedMappedBasesInQ7Alignments) {
        this.extrapolatedMappedBasesInQ7Alignments = extrapolatedMappedBasesInQ7Alignments;
    }

    public int getExtrapolatedQ7LongestAlignment() {
        return extrapolatedQ7LongestAlignment;
    }

    public void setExtrapolatedQ7LongestAlignment(int extrapolatedQ7LongestAlignment) {
        this.extrapolatedQ7LongestAlignment = extrapolatedQ7LongestAlignment;
    }

    public int getExtrapolated50q7Reads() {
        return extrapolated50q7Reads;
    }

    public void setExtrapolated50q7Reads(int extrapolated50q7Reads) {
        this.extrapolated50q7Reads = extrapolated50q7Reads;
    }

    public int getExtrapolated100q7Reads() {
        return extrapolated100q7Reads;
    }

    public void setExtrapolated100q7Reads(int extrapolated100q7Reads) {
        this.extrapolated100q7Reads = extrapolated100q7Reads;
    }

    public int getExtrapolated200q7Reads() {
        return extrapolated200q7Reads;
    }

    public void setExtrapolated200q7Reads(int extrapolated200q7Reads) {
        this.extrapolated200q7Reads = extrapolated200q7Reads;
    }

    public double getExtrapolatedQ10CoveragePercentage() {
        return extrapolatedQ10CoveragePercentage;
    }

    public void setExtrapolatedQ10CoveragePercentage(double extrapolatedQ10CoveragePercentage) {
        this.extrapolatedQ10CoveragePercentage = extrapolatedQ10CoveragePercentage;
    }

    public double getExtrapolatedQ10MeanCoverageDepth() {
        return extrapolatedQ10MeanCoverageDepth;
    }

    public void setExtrapolatedQ10MeanCoverageDepth(double extrapolatedQ10MeanCoverageDepth) {
        this.extrapolatedQ10MeanCoverageDepth = extrapolatedQ10MeanCoverageDepth;
    }

    public int getExtrapolatedQ10Alignments() {
        return extrapolatedQ10Alignments;
    }

    public void setExtrapolatedQ10Alignments(int extrapolatedQ10Alignments) {
        this.extrapolatedQ10Alignments = extrapolatedQ10Alignments;
    }

    public int getExtrapolatedQ10MeanAlignmentLength() {
        return extrapolatedQ10MeanAlignmentLength;
    }

    public void setExtrapolatedQ10MeanAlignmentLength(int extrapolatedQ10MeanAlignmentLength) {
        this.extrapolatedQ10MeanAlignmentLength = extrapolatedQ10MeanAlignmentLength;
    }

    public int getExtrapolatedMappedBasesInQ10Alignments() {
        return extrapolatedMappedBasesInQ10Alignments;
    }

    public void setExtrapolatedMappedBasesInQ10Alignments(int extrapolatedMappedBasesInQ10Alignments) {
        this.extrapolatedMappedBasesInQ10Alignments = extrapolatedMappedBasesInQ10Alignments;
    }

    public int getExtrapolatedQ10LongestAlignment() {
        return extrapolatedQ10LongestAlignment;
    }

    public void setExtrapolatedQ10LongestAlignment(int extrapolatedQ10LongestAlignment) {
        this.extrapolatedQ10LongestAlignment = extrapolatedQ10LongestAlignment;
    }

    public int getExtrapolated50q10Reads() {
        return extrapolated50q10Reads;
    }

    public void setExtrapolated50q10Reads(int extrapolated50q10Reads) {
        this.extrapolated50q10Reads = extrapolated50q10Reads;
    }

    public int getExtrapolated100q10Reads() {
        return extrapolated100q10Reads;
    }

    public void setExtrapolated100q10Reads(int extrapolated100q10Reads) {
        this.extrapolated100q10Reads = extrapolated100q10Reads;
    }

    public int getExtrapolated200q10Reads() {
        return extrapolated200q10Reads;
    }

    public void setExtrapolated200q10Reads(int extrapolated200q10Reads) {
        this.extrapolated200q10Reads = extrapolated200q10Reads;
    }

    public double getExtrapolatedQ17CoveragePercentage() {
        return extrapolatedQ17CoveragePercentage;
    }

    public void setExtrapolatedQ17CoveragePercentage(double extrapolatedQ17CoveragePercentage) {
        this.extrapolatedQ17CoveragePercentage = extrapolatedQ17CoveragePercentage;
    }

    public double getExtrapolatedQ17MeanCoverageDepth() {
        return extrapolatedQ17MeanCoverageDepth;
    }

    public void setExtrapolatedQ17MeanCoverageDepth(double extrapolatedQ17MeanCoverageDepth) {
        this.extrapolatedQ17MeanCoverageDepth = extrapolatedQ17MeanCoverageDepth;
    }

    public int getExtrapolatedQ17Alignments() {
        return extrapolatedQ17Alignments;
    }

    public void setExtrapolatedQ17Alignments(int extrapolatedQ17Alignments) {
        this.extrapolatedQ17Alignments = extrapolatedQ17Alignments;
    }

    public int getExtrapolatedQ17MeanAlignmentLength() {
        return extrapolatedQ17MeanAlignmentLength;
    }

    public void setExtrapolatedQ17MeanAlignmentLength(int extrapolatedQ17MeanAlignmentLength) {
        this.extrapolatedQ17MeanAlignmentLength = extrapolatedQ17MeanAlignmentLength;
    }

    public int getExtrapolatedMappedBasesInQ17Alignments() {
        return extrapolatedMappedBasesInQ17Alignments;
    }

    public void setExtrapolatedMappedBasesInQ17Alignments(int extrapolatedMappedBasesInQ17Alignments) {
        this.extrapolatedMappedBasesInQ17Alignments = extrapolatedMappedBasesInQ17Alignments;
    }

    public int getExtrapolatedQ17LongestAlignment() {
        return extrapolatedQ17LongestAlignment;
    }

    public void setExtrapolatedQ17LongestAlignment(int extrapolatedQ17LongestAlignment) {
        this.extrapolatedQ17LongestAlignment = extrapolatedQ17LongestAlignment;
    }

    public int getExtrapolated50q17Reads() {
        return extrapolated50q17Reads;
    }

    public void setExtrapolated50q17Reads(int extrapolated50q17Reads) {
        this.extrapolated50q17Reads = extrapolated50q17Reads;
    }

    public int getExtrapolated100q17Reads() {
        return extrapolated100q17Reads;
    }

    public void setExtrapolated100q17Reads(int extrapolated100q17Reads) {
        this.extrapolated100q17Reads = extrapolated100q17Reads;
    }

    public int getExtrapolated200q17Reads() {
        return extrapolated200q17Reads;
    }

    public void setExtrapolated200q17Reads(int extrapolated200q17Reads) {
        this.extrapolated200q17Reads = extrapolated200q17Reads;
    }

    public double getExtrapolatedQ20CoveragePercentage() {
        return extrapolatedQ20CoveragePercentage;
    }

    public void setExtrapolatedQ20CoveragePercentage(double extrapolatedQ20CoveragePercentage) {
        this.extrapolatedQ20CoveragePercentage = extrapolatedQ20CoveragePercentage;
    }

    public double getExtrapolatedQ20MeanCoverageDepth() {
        return extrapolatedQ20MeanCoverageDepth;
    }

    public void setExtrapolatedQ20MeanCoverageDepth(double extrapolatedQ20MeanCoverageDepth) {
        this.extrapolatedQ20MeanCoverageDepth = extrapolatedQ20MeanCoverageDepth;
    }

    public int getExtrapolatedQ20Alignments() {
        return extrapolatedQ20Alignments;
    }

    public void setExtrapolatedQ20Alignments(int extrapolatedQ20Alignments) {
        this.extrapolatedQ20Alignments = extrapolatedQ20Alignments;
    }

    public int getExtrapolatedQ20MeanAlignmentLength() {
        return extrapolatedQ20MeanAlignmentLength;
    }

    public void setExtrapolatedQ20MeanAlignmentLength(int extrapolatedQ20MeanAlignmentLength) {
        this.extrapolatedQ20MeanAlignmentLength = extrapolatedQ20MeanAlignmentLength;
    }

    public int getExtrapolatedMappedBasesInQ20Alignments() {
        return extrapolatedMappedBasesInQ20Alignments;
    }

    public void setExtrapolatedMappedBasesInQ20Alignments(int extrapolatedMappedBasesInQ20Alignments) {
        this.extrapolatedMappedBasesInQ20Alignments = extrapolatedMappedBasesInQ20Alignments;
    }

    public int getExtrapolatedQ20LongestAlignment() {
        return extrapolatedQ20LongestAlignment;
    }

    public void setExtrapolatedQ20LongestAlignment(int extrapolatedQ20LongestAlignment) {
        this.extrapolatedQ20LongestAlignment = extrapolatedQ20LongestAlignment;
    }

    public int getExtrapolated50q20Reads() {
        return extrapolated50q20Reads;
    }

    public void setExtrapolated50q20Reads(int extrapolated50q20Reads) {
        this.extrapolated50q20Reads = extrapolated50q20Reads;
    }

    public int getExtrapolated100q20Reads() {
        return extrapolated100q20Reads;
    }

    public void setExtrapolated100q20Reads(int extrapolated100q20Reads) {
        this.extrapolated100q20Reads = extrapolated100q20Reads;
    }

    public int getExtrapolated200q20Reads() {
        return extrapolated200q20Reads;
    }

    public void setExtrapolated200q20Reads(int extrapolated200q20Reads) {
        this.extrapolated200q20Reads = extrapolated200q20Reads;
    }

    public double getExtrapolatedQ47CoveragePercentage() {
        return extrapolatedQ47CoveragePercentage;
    }

    public void setExtrapolatedQ47CoveragePercentage(double extrapolatedQ47CoveragePercentage) {
        this.extrapolatedQ47CoveragePercentage = extrapolatedQ47CoveragePercentage;
    }

    public double getExtrapolatedQ47MeanCoverageDepth() {
        return extrapolatedQ47MeanCoverageDepth;
    }

    public void setExtrapolatedQ47MeanCoverageDepth(double extrapolatedQ47MeanCoverageDepth) {
        this.extrapolatedQ47MeanCoverageDepth = extrapolatedQ47MeanCoverageDepth;
    }

    public int getExtrapolatedQ47Alignments() {
        return extrapolatedQ47Alignments;
    }

    public void setExtrapolatedQ47Alignments(int extrapolatedQ47Alignments) {
        this.extrapolatedQ47Alignments = extrapolatedQ47Alignments;
    }

    public int getExtrapolatedQ47MeanAlignmentLength() {
        return extrapolatedQ47MeanAlignmentLength;
    }

    public void setExtrapolatedQ47MeanAlignmentLength(int extrapolatedQ47MeanAlignmentLength) {
        this.extrapolatedQ47MeanAlignmentLength = extrapolatedQ47MeanAlignmentLength;
    }

    public int getExtrapolatedMappedBasesInQ47Alignments() {
        return extrapolatedMappedBasesInQ47Alignments;
    }

    public void setExtrapolatedMappedBasesInQ47Alignments(int extrapolatedMappedBasesInQ47Alignments) {
        this.extrapolatedMappedBasesInQ47Alignments = extrapolatedMappedBasesInQ47Alignments;
    }

    public int getExtrapolatedQ47LongestAlignment() {
        return extrapolatedQ47LongestAlignment;
    }

    public void setExtrapolatedQ47LongestAlignment(int extrapolatedQ47LongestAlignment) {
        this.extrapolatedQ47LongestAlignment = extrapolatedQ47LongestAlignment;
    }

    public int getExtrapolated50q47Reads() {
        return extrapolated50q47Reads;
    }

    public void setExtrapolated50q47Reads(int extrapolated50q47Reads) {
        this.extrapolated50q47Reads = extrapolated50q47Reads;
    }

    public int getExtrapolated100q47Reads() {
        return extrapolated100q47Reads;
    }

    public void setExtrapolated100q47Reads(int extrapolated100q47Reads) {
        this.extrapolated100q47Reads = extrapolated100q47Reads;
    }

    public int getExtrapolated200q47Reads() {
        return extrapolated200q47Reads;
    }

    public void setExtrapolated200q47Reads(int extrapolated200q47Reads) {
        this.extrapolated200q47Reads = extrapolated200q47Reads;
    }

    public RundbResults getReportId() {
        return reportId;
    }

    public void setReportId(RundbResults reportId) {
        this.reportId = reportId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RundbLibmetrics)) {
            return false;
        }
        RundbLibmetrics other = (RundbLibmetrics) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.iontorrent.dbaccess.RundbLibmetrics[ id=" + id + " ]";
    }
}
